let tests = [];

Object.keys(window.__karma__.files).forEach(function (file) {
  if (/spec\.js$/.test(file)) {
    tests.push(file);
  }
});

require.config({
  baseUrl: '/base/src',

  paths: {
    'players': '../routes/players'
  },

  shim: {
    'underscore': {
      exports: '_'
    }
  },

  deps: tests,

  callback: window.__karma__.start
});
exports = {};
