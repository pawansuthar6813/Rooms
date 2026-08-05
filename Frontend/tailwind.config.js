/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        darkBg: '#090a0f', // Very dark background (login/sidebar)
        darkSurface: '#12141c', // Main dashboard/app area
        darkCard: '#1b1e27', // Elevated cards/panels
        darkBorder: '#272b36', // Subtle borders
        accentGold: '#e3b341', // Gold accent
        accentGoldHover: '#cf9e2f',
      }
    },
  },
  plugins: [],
}

