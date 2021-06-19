const childProcess = require('child_process');
const fs = require('fs-extra');

const startJasmine = function () {
    return require('../buildSrc/test-wrapper')
};

const appPath = process.env.APP_PATH

const appPathComponents = appPath.split(" ")

const serverProcess = childProcess.fork(
    appPathComponents[0],
    appPathComponents.slice(1),
    {stdio: "pipe"}
);

process.on('exit', () => serverProcess.kill());

new Promise(function (resolve, reject) {
    serverProcess.on('message', message => {
        if (message === 'ready') {
            console.log("server ready");
            resolve();
        }
    });
    serverProcess.on('exit', err => {
        console.log("server exit", err);
        reject(err);
    });
    serverProcess.stdout.on("data", (buffer) => {
        const message = buffer.toString("utf8")
        if (message.toString().includes("ready")) {
            console.log("server ready")
            resolve()
        }
    })

    process.stdin.pipe(serverProcess.stdin);

    fs.mkdirSync(__dirname + '/build/reports/tests', {recursive: true});

    const serverOut = fs.createWriteStream(__dirname + '/build/reports/tests/server.out.log');
    const serverErr = fs.createWriteStream(__dirname + '/build/reports/tests/server.err.log');
    serverProcess.stdout.pipe(serverOut);
    serverProcess.stderr.pipe(serverErr);
})
    .then(startJasmine, err => {
        console.log("FAILURE, did not run tests.", err);
        process.exit(1)
    });