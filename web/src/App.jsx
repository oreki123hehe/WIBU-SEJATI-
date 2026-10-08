import { useState } from 'react'
import { Home, Search, Bookmark, History, Play, Bell, Menu } from 'lucide-react'
import './App.css'

const anime = [
  { title: 'Featured Anime', subtitle: 'Dunia Anime, Satu Tempat.' },
  { title: 'Anime Terbaru', subtitle: 'Episode terbaru setiap saat.' },
  { title: 'Anime Populer', subtitle: 'Yang sedang banyak ditonton.' },
]

function App() {
  const [page, setPage] = useState('home')

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">WIBU SEJATI</div>
        <div className="top-actions">
          <Bell size={21} />
          <Menu size={22} />
        </div>
      </header>

      <main className="content">
        {page === 'home' && (
          <>
            <section className="hero">
              <div>
                <span className="badge">WIBU SEJATI</span>
                <h1>Dunia Anime,<br />Satu Tempat.</h1>
                <p>Temukan anime favoritmu dan lanjutkan tontonanmu.</p>
                <button><Play size={18} fill="currentColor" /> Mulai Menonton</button>
              </div>
            </section>

            <section>
              <div className="section-title">
                <h2>Jelajahi Anime</h2>
                <span>Lihat semua</span>
              </div>

              <div className="anime-grid">
                {anime.map((item) => (
                  <article className="anime-card" key={item.title}>
                    <div className="poster">
                      <Play size={28} />
                    </div>
                    <h3>{item.title}</h3>
                    <p>{item.subtitle}</p>
                  </article>
                ))}
              </div>
            </section>
          </>
        )}

        {page === 'explore' && (
          <section className="page">
            <h1>Explore</h1>
            <div className="search">
              <Search size={20} />
              <input placeholder="Cari anime..." />
            </div>
          </section>
        )}

        {page === 'watchlist' && (
          <section className="page">
            <h1>Watchlist</h1>
            <p>Anime yang kamu simpan akan muncul di sini.</p>
          </section>
        )}

        {page === 'history' && (
          <section className="page">
            <h1>History</h1>
            <p>Riwayat tontonanmu akan muncul di sini.</p>
          </section>
        )}
      </main>

      <nav className="bottom-nav">
        <button className={page === 'home' ? 'active' : ''} onClick={() => setPage('home')}>
          <Home size={21} /><span>Home</span>
        </button>
        <button className={page === 'explore' ? 'active' : ''} onClick={() => setPage('explore')}>
          <Search size={21} /><span>Explore</span>
        </button>
        <button className={page === 'watchlist' ? 'active' : ''} onClick={() => setPage('watchlist')}>
          <Bookmark size={21} /><span>Watchlist</span>
        </button>
        <button className={page === 'history' ? 'active' : ''} onClick={() => setPage('history')}>
          <History size={21} /><span>History</span>
        </button>
      </nav>
    </div>
  )
}

export default App
