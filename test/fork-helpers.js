const Promise = require('bluebird');
const childProcess = require('child_process');

function forkJasmine(specDirectory, tempDirectory, testFilePath, jasmineSavePath, filePrefix) {

  let process = undefined;
  const promise = new Promise(function (resolve, reject) {
    process = childProcess.fork(__dirname + '/forkJasmine', [specDirectory, tempDirectory, testFilePath, jasmineSavePath, filePrefix]);

    process.on('exit', function (code) {
      if (code === 0)
        resolve(code);
      else {
        reject(code);
      }
    });
  });
  return {
    promise: promise,
    process: process
  };
}

module.exports = {
  forkJasmine: forkJasmine
};