declare module '*.pug' {
    var _: (model: any) => string;
    export =  _;
}

declare module '*.json' {
    var _: any;

    export = _;
}

declare module '*.css' {
    var _: any;

    export = _;
}

declare module 'protractor-jasmine2-screenshot-reporter' {
    export = class {
        constructor(options: any)
    }
}