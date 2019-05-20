declare module '*.pug' {
    let _: (model: any) => string;
    export = _;
}

declare module '*.json' {
    let _: any;

    export = _;
}

declare module '*.css' {
    let _: any;

    export = _;
}

declare module 'protractor-jasmine2-screenshot-reporter' {
    export = class {
        constructor(options: any)
    }
}

// noinspection ES6UnusedImports
import IPromise = angular.IPromise;