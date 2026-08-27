/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          DEFAULT: '#1E2761',
          light: '#2A3680',
          dark: '#141A43',
        },
        teal: {
          DEFAULT: '#00A896',
          hover: '#009081',
          light: '#E6F6F4',
          dark: '#028090',
        },
        brandGray: {
          light: '#F4F6FB',
          border: '#E2E8F0',
          text: '#5B6472',
        },
        status: {
          success: '#00A896',
          error: '#E45858',
          warning: '#F0A93C',
        }
      },
      fontFamily: {
        serif: ['Georgia', 'Playfair Display', 'serif'],
        sans: ['Inter', 'Calibri', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
