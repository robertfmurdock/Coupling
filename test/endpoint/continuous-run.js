const webpackRunner = require('../webpackRunner');
const serverWebpackConfig = require('../../server/webpack.config');
const testWebpackConfig = require('./webpack.config');
const childProcess = require('child_process');
const Promise = require('bluebird');
const _ = require('underscore');
const runHelpers = require('../run-helpers');
const forkHelpers = require('./../fork-helpers');

const forkOptions = {
  env: _.extend({
    PORT: 3001,
    NODE_ENV: 'test',
    DISABLE_LOGGING: true
  }, process.env)
};

function forkJasmine() {
  return forkHelpers.forkJasmine('test/endpoint', '.tmp', 'test.js', __dirname + '/../../../test-output', 'endpoint.xml',
    forkOptions);
}

const removeTempDirectory = function () {
  runHelpers.removeTempDirectory(__dirname + '/.tmp')
};

let appProcesses;

const appWatcher = webpackRunner.watch(serverWebpackConfig, function () {
  if (!appProcesses) {
    appProcesses = startForkedAppAndWatchTests();
  } else {
    appProcesses.forEach(app => app.kill('SIGINT'));

    appProcesses = startForkedAppAndWatchTests();
  }
});

let startForkedApp = function () {
  const appProcess1 = childProcess.fork('./test/endpoint/startForkedApp', forkOptions);
  const appProcess2 = childProcess.fork('./test/endpoint/startForkedApp');
  const appProcesses = [appProcess1, appProcess2];
  const appIsReadyPromise = new Promise(function (resolve, reject) {
    appProcess1.on('message', function (json) {
      if (json.message === 'Application Ready') {
        resolve(json);
      } else {
        reject(json);
      }
    })
  });
  return {appProcesses, appIsReadyPromise};
};

const startForkedAppAndWatchTests = function () {
  const {appProcesses, appIsReadyPromise} = startForkedApp();
  appIsReadyPromise
    .then(function () {
      let testRunPromise = undefined;
      let process = undefined;
      const testWatcher = webpackRunner.watch(testWebpackConfig, function () {
        if (testRunPromise) {
          testRunPromise = testRunPromise
            .then(function () {
              const forkInfo = forkJasmine();
              process = forkInfo.process;
              return forkInfo.promise;
            })
            .catch(function(err) {
              console.log('Tests crashed: ', err);
              return true;
            })
        } else {
          const forkInfo = forkJasmine();
          testRunPromise = forkInfo.promise;
          process = forkInfo.process;
        }
      });

      appProcesses.forEach(app => {
        app.on('exit', function () {
          testRunPromise.catch(function () {
            return 'All good';
          });
          process.kill('SIGINT');
          testWatcher.close();
        })
      })

    });
  return appProcesses;
};


process.on('exit', function () {
  console.log("Caught interrupt signal");
  removeTempDirectory();
  appWatcher.close();
});