module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-env');
    grunt.loadNpmTasks('grunt-mkdir');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-mocha-test');
    grunt.loadNpmTasks('grunt-express-server');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-git-describe');

    grunt.initConfig({
        env: {
            jenkinsUnit: {
                JUNIT_REPORT_PATH: 'test-output/unit.xml'
            },
            jenkinsEndpoint: {
                JUNIT_REPORT_PATH: 'test-output/endpoint.xml'
            }
        },
        mkdir: {
            testOutput: {
                options: {
                    create: ['test-output']
                }
            }
        },
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS'],
                reporters: ['dots']
            },
            jenkins: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS'],
                reporters: ['junit'],
                junitReporter: {
                    outputFile: 'test-output/test-results.xml'
                }
            }
        },
        mochaTest: {
            unit: {
                options: {
                    reporter: 'spec'
                },
                src: ['test/mocha/**/*.js']
            },
            jenkinsUnit: {
                options: {
                    reporter: 'mocha-jenkins-reporter'
                },
                src: ['test/mocha/**/*.js']
            },
            endpoint: {
                options: {
                    reporter: 'spec'
                },
                src: ['test/endpoint/**/*.js']
            },
            jenkinsEndpoint: {
                options: {
                    reporter: 'mocha-jenkins-reporter'
                },
                src: ['test/endpoint/**/*.js']
            }
        },
        express: {
            options: {},
            dev: {
                options: {
                    script: 'app.js',
                    port: 3001,
                    //output: 'Finished initializing session storage'
                    delay: 4000
                }

            }
        },
        protractor_webdriver: {
            options: {keepAlive: true},
            start: {}
        },
        protractor: {
            options: {
                configFile: "test/e2e/protractor-conf.js",
                keepAlive: false,
                noColor: false,
                args: {
                    browser: 'chrome'
                }
            },
            e2e: {},
            jenkins: {
                options: {
                    args: {
                        browser: 'chrome'
                    }
                }
            }
        },
        "git-describe": {
            "options": {},
            "jenkins": {}
        }
    });

    grunt.registerTask('saveRevision', function () {
            grunt.event.once('git-describe', function (rev) {
                var info = {
                    gitRev: rev.toString(),
                    date: new Date()
                };
                grunt.file.write('version.json', JSON.stringify(info));
                grunt.option('gitRevision', rev);
            });
            grunt.task.run('git-describe');
        }
    );
    grunt.registerTask('markAsDevelopmentBuild', function () {
        grunt.file.write('version.json', JSON.stringify({gitRev: 'DEVELOPMENT'}));
    });

    grunt.registerTask('jenkinsMochaUnit', ['env:jenkinsUnit', 'mochaTest:jenkinsUnit']);
    grunt.registerTask('jenkinsMochaEndpoint', ['env:jenkinsEndpoint', 'mochaTest:jenkinsEndpoint']);

    grunt.registerTask('default', ['mochaTest:unit', 'karma:unit', 'express:dev', 'mochaTest:endpoint',
        'protractor_webdriver:start', 'protractor:e2e', 'markAsDevelopmentBuild']);
    grunt.registerTask('jenkins', ['mkdir:testOutput', 'jenkinsMochaUnit', 'karma:jenkins', 'express:dev', 'jenkinsMochaEndpoint', 'saveRevision']);

}
;