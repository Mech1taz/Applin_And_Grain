/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        crema: '#F2EDD0',        // fondo claro / texto oscuro
        cafe: '#401A1A',         // fondo oscuro
        oliva: '#6A7349',        // cards en modo oscuro
        'oliva-claro': '#808C54',// texto claro / acento oscuro
        ocre: '#D9A362',         // CTA claro
        terracota: '#BF5050',    // alertas claro
        'terracota-muted': '#A64141', // CTA oscuro / alertas oscuro
        lima: '#E3F26D',         // acento claro
      },
      fontFamily: {
        display: ['"Fraunces"', 'Georgia', 'serif'],
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
