const fs = require('fs-extra');
const Promise = require('bluebird');
const webpackRunner = require('../webpackRunner');
const webpackConfig = require('./webpack.config');

const childProcess = require('child_process');

process.env.PORT = 3001;

const serverProcess = childProcess.fork(__dirname + '/forkStartup', [], {stdio: "pipe"});

let promise = Promise.all([
  new Promise(function (resolve, reject) {
    serverProcess.on('message', message => {
      if (message === 'ready') {
        console.log("server ready");
        resolve();
      }
    });
    serverProcess.on('exit', err => {
      console.log("server exit");
      reject(err);
    });

    process.stdin.pipe(serverProcess.stdin);

    fs.mkdirSync(__dirname + '/../../build/test-results/e2e', { recursive: true });

    const serverOut = fs.createWriteStream(__dirname + '/../../build/test-results/e2e/server.out.log');
    const serverErr = fs.createWriteStream(__dirname + '/../../build/test-results/e2e/server.err.log');
    serverProcess.stdout.pipe(serverOut);
    serverProcess.stderr.pipe(serverErr);
  }),
  webpackRunner.run(webpackConfig)
]);

promise = promise
  .then(function () {
    return new Promise(function (resolve, reject) {
      const process = childProcess.fork(__dirname + '/forkProtractor');

      process.on('exit', function (code) {
        console.log('protractor fork code ' + code);
        if (code === 0)
          resolve(code);
        else {
          reject(code);
        }
      });
    });
  });

promise.finally(function () {
  fs.removeSync(__dirname + '/.tmp');
})
  .then(function (exitCode) {
    serverProcess.kill();
    process.exit(exitCode);
  }, function (err) {
    console.log('Error', err);
    serverProcess.kill();
    process.exit(-1);
  });