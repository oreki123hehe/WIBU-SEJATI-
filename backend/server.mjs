import http from "node:http";
import {readFile,writeFile,mkdir} from "node:fs/promises";
import {join} from "node:path";
import {fileURLToPath} from "node:url";

const ROOT=fileURLToPath(new URL(".",import.meta.url));
const PORT=Number(process.env.PORT||8787);
const JIKAN=process.env.JIKAN_BASE||"https://api.jikan.moe/v4";
const STATE=join(ROOT,"data","state.json");
const cache=new Map();

async function ensure(){
  await mkdir(join(ROOT,"data"),{recursive:true});
  try{await readFile(STATE,"utf8")}catch{await writeFile(STATE,JSON.stringify({followedAnime:[],lastKnownEpisode:{},events:[]},null,2));}
}
async function readState(){await ensure();return JSON.parse(await readFile(STATE,"utf8"))}
async function saveState(s){await writeFile(STATE,JSON.stringify(s,null,2)+"\n")}
async function jikan(path){
  const c=cache.get(path);if(c&&Date.now()-c.t<60000)return c.v;
  const r=await fetch(JIKAN+path,{headers:{Accept:"application/json","User-Agent":"WibuSejati/0.1"}});
  if(!r.ok)throw Error("Jikan HTTP "+r.status);
  const v=await r.json();cache.set(path,{t:Date.now(),v});return v;
}
function send(res,code,data){res.writeHead(code,{"Content-Type":"application/json; charset=utf-8","Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type"});res.end(JSON.stringify(data));}
async function route(req,res){
  const u=new URL(req.url,"http://localhost"),p=u.pathname;
  if(req.method==="OPTIONS"){res.writeHead(204,{"Access-Control-Allow-Origin":"*"});return res.end();}
  try{
    if(p==="/health")return send(res,200,{ok:true,service:"wibu-sejati",time:new Date().toISOString()});
    if(p==="/api/home"){const [top,now]=await Promise.all([jikan("/top/anime?limit=12"),jikan("/seasons/now?limit=12")]);return send(res,200,{top:top.data||[],now:now.data||[]});}
    if(p==="/api/anime/search"){const q=(u.searchParams.get("q")||"").trim();if(!q)return send(res,400,{error:"q required"});return send(res,200,await jikan("/anime?q="+encodeURIComponent(q)+"&limit=20&sfw=true"));}
    let m=p.match(/^\/api\/anime\/(\d+)$/);
    if(m)return send(res,200,await jikan("/anime/"+m[1]+"/full"));
    m=p.match(/^\/api\/anime\/(\d+)\/episodes$/);
    if(m){const page=Number(u.searchParams.get("page")||1);return send(res,200,await jikan("/anime/"+m[1]+"/episodes?page="+page));}
    if(p==="/api/events"){const s=await readState();return send(res,200,{events:s.events||[]});}
    return send(res,404,{error:"Not found"});
  }catch(e){return send(res,502,{error:e.message||"upstream error"});}
}
async function worker(){
  await ensure();const s=await readState();
  for(const id of s.followedAnime||[]){
    try{
      const j=await jikan("/anime/"+id+"/episodes?page=1");
      const latest=(j.data||[]).sort((a,b)=>(b.mal_id||0)-(a.mal_id||0))[0];if(!latest)continue;
      const key=String(id),old=Number(s.lastKnownEpisode[key]||0),cur=Number(latest.mal_id||0);
      if(cur>old){s.lastKnownEpisode[key]=cur;s.events=[{id:Date.now(),animeId:id,episode:cur,title:latest.title||("Episode "+cur),createdAt:new Date().toISOString()},...(s.events||[])].slice(0,100);console.log("NEW EPISODE",id,cur);}
    }catch(e){console.error("worker",id,e.message)}
  }
  await saveState(s);console.log("sync complete");
}
await ensure();
if(process.argv.includes("--worker")){await worker();setInterval(worker,Number(process.env.POLL_INTERVAL_MS||900000));}
else http.createServer(route).listen(PORT,"0.0.0.0",()=>console.log("Wibu Sejati backend :"+PORT));
