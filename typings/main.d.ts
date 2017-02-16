/// <reference path="main/ambient/angular-resource/index.d.ts" />
/// <reference path="main/ambient/angular-route/index.d.ts" />
/// <reference path="main/ambient/angular/index.d.ts" />
/// <reference path="main/ambient/jquery/index.d.ts" />

declare module '*.pug' {
    var _: string;
    export default  _;
}

declare module 'protractor-jasmine2-screenshot-reporter' {
    export = class {
        constructor(options: any)
    }
}