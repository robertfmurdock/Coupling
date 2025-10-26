import {defineConfig} from 'vite'
import {createHtmlPlugin} from 'vite-plugin-html'
import path from 'path'
import fs from 'fs'

export default defineConfig(({command}) => {
    let cdnFile = fs.readFileSync(path.resolve(__dirname, '../../../../client/build/cdn.json'), {encoding: "UTF-8"});
    let isServing = command === 'serve';
    if (isServing) {
        cdnFile = cdnFile.replaceAll('production', 'development')
    }
    const cdnResources = JSON.parse(
        cdnFile
            .split(/\r?\n/)
            .filter((line) => !line.includes("TRACE"))
            .join("\n")
    )
    const cdnSettings = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../../../client/cdn.settings.json')))
    const cdnImportMap = Object.fromEntries(
        Object.entries(cdnSettings).map(([key, value]) => [key, cdnResources[key]])
    );
    return ({
        root: "kotlin",
        server: {
            port: 3000
        },
        build: {
            rollupOptions: {
                external: Object.keys(cdnSettings),
                output: {
                    globals: cdnSettings,
                },
            },
        },
        assetsInclude: [
            "**/*.md"
        ],
        optimizeDeps: {
            exclude: [

            ]
        },
        experimental: {
            renderBuiltUrl(filename, {hostType}) {
                if (hostType === 'js') {
                    return {runtime: `window.webpackPublicPath + ${JSON.stringify(filename)}`}
                } else {
                    return {relative: true}
                }
            },
        },
        plugins: [
            createHtmlPlugin({
                minify: true,
                template: 'index.html',
                inject: {
                    data: {
                        htmlWebpackPlugin: {
                            options: {
                                appMountClass: 'view-container',
                                cdnSettings: cdnSettings,
                                cdnImportMap: cdnImportMap,
                                window: isServing ? {
                                    expressEnv: "dev",
                                    inMemory: true,
                                    auth0ClientId: "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg",
                                    auth0Domain: "zegreatrob.us.auth0.com",
                                    basename: '',
                                    prereleaseMode: true,
                                } : {}
                            },
                            tags: {headTags: `<script type="module" src="./Coupling-client.mjs"></script>`}
                        }
                    },
                }
            }),
        ],
    });
})
