import { Shield } from 'lucide-react';

export default function Header() {
  return (
    <header className="bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg">
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            {/* TruData Logo */}
            <div className="flex items-center">
              <svg width="40" height="40" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{ stopColor: '#ffffff', stopOpacity: 1 }} />
                    <stop offset="100%" style={{ stopColor: '#e0f2fe', stopOpacity: 1 }} />
                  </linearGradient>
                </defs>
                <path d="M8 10 L20 5 L32 10 L32 22 Q32 28 20 33 Q8 28 8 22 Z" 
                      fill="url(#logoGrad)" stroke="white" strokeWidth="1.5"/>
                <path d="M13 20 L17 24 L27 13" 
                      stroke="#0ea5e9" strokeWidth="2.5" fill="none" 
                      strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </div>
            
            <div>
              <h1 className="text-3xl font-bold tracking-tight">
                Tru<span className="text-blue-200">Data</span>
              </h1>
              <p className="text-sm text-blue-100 tracking-wide">
                DATA QUALITY ASSURANCE PLATFORM
              </p>
            </div>
          </div>
          
          <div className="flex items-center space-x-2 bg-white/10 px-4 py-2 rounded-lg backdrop-blur-sm">
            <Shield className="w-5 h-5" />
            <span className="text-sm font-medium">Secure & Private</span>
          </div>
        </div>
      </div>
    </header>
  );
}

