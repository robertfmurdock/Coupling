module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-env');
    grunt.loadNpmTasks('grunt-mkdir');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-mocha-test');
    grunt.loadNpmTasks('grunt-express-server');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-git-describe');
    grunt.loadNpmTasks('grunt-wiredep');

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
                browsers: ['PhantomJS', 'Chrome', 'Firefox'],
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
            },
            travis: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS', 'Firefox'],
                reporters: ['dots']
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
                    script: 'server/app.js',
                    port: 3001,
                    output: 'Finished initializing session storage'
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
            chrome: {
                options: {
                    args: {
                        browser: 'chrome'
                    }
                }
            },
            firefox: {
                options: {
                    args: {
                        browser: 'firefox'
                    }
                }
            },
            safari: {
                options: {
                    args: {
                        browser: 'safari'
                    }
                }
            }
        },
        "git-describe": {
            "options": {},
            "jenkins": {}
        },
        wiredep: {
            productionTask: {
                src: [
                    'views/layout.jade'
                ],
                options: {
                    ignorePath: '../public'
                }
            },
            karmaTask: {
                src: [
                    'karma.conf.js'
                ],
                fileTypes: {
                    js: {
                        block: /(([\s\t]*)\/\/\s*bower:*(\S*))(\n|\r|.)*?(\/\/\s*endbower)/gi,
                        detect: {
                            js: /'(.*\.js)'/gi
                        },
                        replace: {
                            js: '\'{{filePath}}\','
                        }
                    }
                },
                options: {
                    devDependencies: true
                }
            }
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
    grunt.registerTask('unit', ['mochaTest:unit', 'karma:unit']);
    grunt.registerTask('jenkinsMochaUnit', ['env:jenkinsUnit', 'mochaTest:jenkinsUnit']);
    grunt.registerTask('jenkinsMochaEndpoint', ['env:jenkinsEndpoint', 'mochaTest:jenkinsEndpoint']);

    grunt.registerTask('end2end', ['express:dev', 'protractor:chrome']);

    grunt.registerTask('default', ['unit', 'express:dev', 'mochaTest:endpoint',
        'protractor_webdriver:start', 'protractor:chrome', 'protractor:firefox', 'markAsDevelopmentBuild']);
    grunt.registerTask('jenkins', ['mkdir:testOutput', 'jenkinsMochaUnit', 'karma:jenkins', 'express:dev', 'jenkinsMochaEndpoint', 'saveRevision']);
    grunt.registerTask('travis', ['mkdir:testOutput', 'jenkinsMochaUnit', 'karma:travis', 'express:dev', 'jenkinsMochaEndpoint', 'saveRevision']);
}
;
