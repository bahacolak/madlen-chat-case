import React from 'react';
import { Link } from 'react-router-dom';

export const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      {/* Navigation */}
      <nav className="border-b border-zinc-800 bg-zinc-900/50 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-zinc-800 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-zinc-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                  />
                </svg>
              </div>
              <span className="text-xl font-bold text-zinc-100">Madlen Chat</span>
            </div>
            <div className="flex items-center gap-4">
              <Link
                to="/login"
                className="text-sm text-zinc-400 hover:text-zinc-100 transition-colors"
              >
                Giriş Yap
              </Link>
              <Link
                to="/register"
                className="px-4 py-2 bg-zinc-100 text-zinc-900 rounded-lg hover:bg-white transition-colors font-medium text-sm"
              >
                Başla
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-20 pb-32 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <div className="text-center">
            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold mb-6 bg-gradient-to-r from-zinc-100 via-zinc-300 to-zinc-100 bg-clip-text text-transparent">
              AI ile Konuşun
            </h1>
            <p className="text-xl md:text-2xl text-zinc-400 mb-8 max-w-3xl mx-auto">
              OpenRouter üzerinden çeşitli AI modelleriyle sohbet edin. GPT-4, Gemini, Claude ve daha fazlası.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                to="/register"
                className="px-8 py-4 bg-zinc-100 text-zinc-900 rounded-xl hover:bg-white transition-all font-semibold text-lg shadow-lg hover:shadow-xl"
              >
                Ücretsiz Başla
              </Link>
              <Link
                to="/login"
                className="px-8 py-4 bg-zinc-800 border border-zinc-700 text-zinc-100 rounded-xl hover:bg-zinc-700 transition-all font-semibold text-lg"
              >
                Giriş Yap
              </Link>
            </div>
          </div>

          {/* Hero Image/Preview */}
          <div className="mt-16 relative">
            <div className="bg-zinc-900 border border-zinc-800 rounded-2xl p-8 shadow-2xl max-w-4xl mx-auto">
              <div className="space-y-4">
                <div className="flex gap-4 justify-end">
                  <div className="bg-zinc-800 rounded-2xl px-4 py-3 max-w-md">
                    <p className="text-sm text-zinc-200">React ile nasıl state yönetimi yapabilirim?</p>
                  </div>
                </div>
                <div className="flex gap-4 justify-start">
                  <div className="w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center flex-shrink-0">
                    <svg
                      className="w-5 h-5 text-zinc-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                      />
                    </svg>
                  </div>
                  <div className="bg-zinc-800 rounded-2xl px-4 py-3 max-w-lg">
                    <p className="text-sm text-zinc-200">
                      React'te state yönetimi için birkaç yaklaşım var: useState, useReducer, Context API ve external libraries...
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 px-4 sm:px-6 lg:px-8 bg-zinc-900/50">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4 text-zinc-100">Özellikler</h2>
            <p className="text-xl text-zinc-400 max-w-2xl mx-auto">
              Modern AI sohbet deneyimi için ihtiyacınız olan her şey
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition-colors">
              <div className="w-12 h-12 rounded-lg bg-zinc-800 flex items-center justify-center mb-4">
                <svg
                  className="w-6 h-6 text-zinc-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Çoklu Model Desteği</h3>
              <p className="text-zinc-400">
                GPT-4, Gemini, Claude ve daha fazlası. İstediğiniz AI modelini seçin ve sohbet edin.
              </p>
            </div>

            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition-colors">
              <div className="w-12 h-12 rounded-lg bg-zinc-800 flex items-center justify-center mb-4">
                <svg
                  className="w-6 h-6 text-zinc-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Geçmiş Yönetimi</h3>
              <p className="text-zinc-400">
                Tüm konuşmalarınızı kaydedin, düzenleyin ve istediğiniz zaman geri dönün.
              </p>
            </div>

            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 hover:border-zinc-700 transition-colors">
              <div className="w-12 h-12 rounded-lg bg-zinc-800 flex items-center justify-center mb-4">
                <svg
                  className="w-6 h-6 text-zinc-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Multi-Modal Desteği</h3>
              <p className="text-zinc-400">
                Resim yükleyin ve AI ile görsel içerik hakkında konuşun.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-24 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4 text-zinc-100">Nasıl Çalışır?</h2>
            <p className="text-xl text-zinc-400 max-w-2xl mx-auto">
              Üç basit adımda başlayın
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-zinc-800 text-zinc-300 text-2xl font-bold mb-4">
                1
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Hesap Oluştur</h3>
              <p className="text-zinc-400">
                Ücretsiz hesap oluşturun ve hemen başlayın
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-zinc-800 text-zinc-300 text-2xl font-bold mb-4">
                2
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Model Seç</h3>
              <p className="text-zinc-400">
                İstediğiniz AI modelini seçin ve sohbeti başlatın
              </p>
            </div>

            <div className="text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-zinc-800 text-zinc-300 text-2xl font-bold mb-4">
                3
              </div>
              <h3 className="text-xl font-semibold mb-2 text-zinc-100">Sohbet Et</h3>
              <p className="text-zinc-400">
                Sorularınızı sorun ve AI'dan anında yanıt alın
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 px-4 sm:px-6 lg:px-8 bg-zinc-900/50">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-bold mb-6 text-zinc-100">
            Hemen Başlayın
          </h2>
          <p className="text-xl text-zinc-400 mb-8">
            AI ile sohbet etmeye başlamak için ücretsiz hesap oluşturun
          </p>
          <Link
            to="/register"
            className="inline-block px-8 py-4 bg-zinc-100 text-zinc-900 rounded-xl hover:bg-white transition-all font-semibold text-lg shadow-lg hover:shadow-xl"
          >
            Ücretsiz Başla
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-zinc-800 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-zinc-800 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-zinc-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                  />
                </svg>
              </div>
              <span className="text-lg font-semibold text-zinc-100">Madlen Chat</span>
            </div>
            <p className="text-sm text-zinc-500">
              © 2024 Madlen Chat. Tüm hakları saklıdır.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

