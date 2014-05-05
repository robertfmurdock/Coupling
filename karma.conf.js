// Karma configuration
// Generated on Sun Mar 23 2014 15:13:25 GMT-0400 (EDT)

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            {pattern: 'node_modules/requirejs/**/*.js', included: false},
            "public/javascripts/bower_components/angular/angular.js",
            "public/javascripts/bower_components/angular-route/angular-route.min.js",
            "public/javascripts/bower_components/underscore/underscore.js",
            "public/javascripts/bower_components/angular-gravatar/build/angular-gravatar.js",
            "public/javascripts/bower_components/angular-gravatar/build/md5.js",
            "public/javascripts/bower_components/angular-animate/angular-animate.min.js",
            "public/javascripts/prefixfree.min.js",
            "public/javascripts/draganddrop.js",
            "public/app/app.js",
            "public/app/services.js",
            "public/app/controllers.js",
            "public/app/animations.js",
            "public/app/filters.js",

            "public/javascripts/bower_components/angular-mocks/angular-mocks.js",
            "public/javascripts/bower_components/jasmine/lib/jasmine-core/jasmine.js",
            'test/jasmine/**/*-spec.js'
        ],

        // list of files to exclude
        exclude: [

        ],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {

        },


        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['Chrome'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false
    });
};
