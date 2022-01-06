const startJasmine = function () {
    return require('../buildSrc/test-wrapper')
};

new Promise(function (resolve) {
    resolve()
})
    .then(startJasmine, err => {
        console.log("FAILURE, did not run tests.", err);
        process.exit(1)
    });