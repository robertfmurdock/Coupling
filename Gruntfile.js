module.exports = function(grunt) {

    grunt.loadNpmTasks('grunt-env');
    grunt.loadNpmTasks('grunt-mkdir');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-mocha-test');
    grunt.loadNpmTasks('grunt-express-server');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-git-describe');
    grunt.loadNpmTasks('grunt-wiredep');
    grunt.loadNpmTasks('grunt-contrib-watch');

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
            docker: {
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
            },
            travis: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS', 'Firefox'],
                reporters: ['dots', 'junit'],
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
                    script: 'server/app.js',
                    port: 3001,
                    output: 'Express server listening on port'
                }
            },
            dev2: {
                options: {
                    script: 'server/app.js',
                    port: 3000,
                    output: 'Express server listening on port'
                }
            }
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
            dockerfirefox: {
                options: {
                    args: {
                        browser: 'firefox',
                        seleniumAddress: 'http://hub:4444/wd/hub'
                    }
                }
            },
            dockerchrome: {
                options: {
                    args: {
                        browser: 'chrome',
                        seleniumAddress: 'http://hub:4444/wd/hub'
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
        watch: {
            //gruntfile: {
            //    files: ['Gruntfile.js']
            //},
            servertest: {
                files: [
                    "server/**/*",
                    "test/endpoint/**/*",
                    "test/mocha/**/*"
                ],
                tasks: ['docker-server-test'],
                options: {
                    interval: 5007
                }
            },
            frontendtest: {
                files: [
                    "public/app/**/*",
                    "public/images/**/*",
                    "public/stylesheets/**/*",
                    "server/**/*",
                    "views/**/*",
                    "!**/*.css",
                    "test/jasmine/**/*",
                    "test/e2e/**/*"
                ],
                tasks: ['docker-frontend-test'],
                options: {
                    interval: 5007
                }
            }
            // ,
            // express: {
            //     files: [
            //         "public/app/**/*",
            //         "public/images/**/*",
            //         "public/stylesheets/**/*",
            //         "server/**/*",
            //         "views/**/*",
            //         "!**/*.css"
            //     ],
            //     tasks: ['express:dev2', 'wait'],
            //     options: {
            //         livereload: true,
            //         nospawn: true,
            //         interval: 5007
            //     }
            // }
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

    grunt.registerTask('saveRevision', function() {
        grunt.event.once('git-describe', function(rev) {
            var info = {
                gitRev: rev.toString(),
                date: new Date()
            };
            grunt.file.write('version.json', JSON.stringify(info));
            grunt.option('gitRevision', rev);
        });
        grunt.task.run('git-describe');
    });
    grunt.registerTask('markAsDevelopmentBuild', function() {
        grunt.file.write('version.json', JSON.stringify({
            gitRev: 'DEVELOPMENT'
        }));
    });
    grunt.registerTask('unit', ['mochaTest:unit', 'karma:unit']);
    grunt.registerTask('jenkinsMochaUnit', ['env:jenkinsUnit', 'mochaTest:jenkinsUnit']);
    grunt.registerTask('jenkinsMochaEndpoint', ['env:jenkinsEndpoint', 'mochaTest:jenkinsEndpoint']);

    grunt.registerTask('end2end', ['express:dev', 'protractor:chrome']);

    grunt.registerTask('default', ['unit', 'express:dev', 'mochaTest:endpoint',
        'protractor:chrome', 'protractor:firefox', 'markAsDevelopmentBuild'
    ]);
    grunt.registerTask('jenkins', ['mkdir:testOutput', 'jenkinsMochaUnit', 'karma:jenkins', 'express:dev', 'jenkinsMochaEndpoint', 'saveRevision']);
    grunt.registerTask('travis', ['mkdir:testOutput', 'jenkinsMochaUnit', 'karma:travis', 'express:dev', 'jenkinsMochaEndpoint', 'saveRevision']);
    grunt.registerTask('serve', ['jenkinsMochaUnit', 'karma:jenkins', 'express:dev', 'jenkinsMochaEndpoint', 'express:dev', 'watch']);

    grunt.registerTask('docker-server-test', ['mochaTest:unit', 'express:dev', 'mochaTest:endpoint']);
    grunt.registerTask('docker-frontend-test', ['karma:docker', 'express:dev', 'protractor:dockerfirefox']);
    grunt.registerTask('dockerserve', ['docker-server-test', 'docker-frontend-test', 'express:dev2', 'watch']);

    grunt.registerTask('wait', function() {
        grunt.log.ok('Waiting for server reload...');
        var done = this.async();
        setTimeout(function() {
            grunt.log.writeln('Done waiting!');
            done();
        }, 1500);
    });
};