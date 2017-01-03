var webpack = require('webpack');
var Promise = require('bluebird');

module.exports = {
  run: function (webpackConfig) {
    return new Promise(function (resolve, reject) {
      webpack(webpackConfig)
        .run(function (err, stats) {
          console.log(stats.toString('minimal'));
          if (err) {
            reject(err);
          }
          console.log('Starting tests:');
          resolve();
        });
    })
  },
  watch: function(webpackConfig, handler, fs){
    const compiler = webpack(webpackConfig);
    if(fs) {
      compiler.outputFileSystem = fs;
    }
    return compiler.watch({}, handler);
  }
};