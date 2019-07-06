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
    // @ts-ignore
    export = class {
        constructor(options: any)
    }
}