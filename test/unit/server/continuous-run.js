const runHelpers = require('./run-helpers');
const webpackRunner = require('../../webpackRunner');
var config = require('./webpack.config');

const startJasmine = runHelpers.startJasmine;
const removeTempDirectory = runHelpers.removeTempDirectory;


var testRun = undefined;

webpackRunner.watch(config, function (err, stats) {
  console.log('stats', stats.toString('minimal'));
  if (!err) {

    if (testRun) {
      testRun.then(function () {
        return startJasmine();
      })
    } else {
      testRun = startJasmine();
    }

  } else {
    console.log(err);
  }

});