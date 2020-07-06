const webpack = require('webpack');
const Promise = require('bluebird');

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
  watch: function (webpackConfig, handler, fs) {
    const compiler = webpack(webpackConfig);
    if (fs) {
      compiler.outputFileSystem = fs;
    }
    let hash = undefined;
    return compiler.watch({}, function (err, stats) {
      const newHash = stats.toJson().hash;
      if (!err && hash !== newHash) {
        hash = newHash;
        console.log('stats', stats.toString('minimal'));
        return handler.apply(this, arguments);
      }

      if (err) {
        console.log(err);
      }
    });
  }
};