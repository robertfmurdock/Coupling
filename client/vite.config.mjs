import fs from 'fs'
import fsp from 'fs/promises'
import path from 'path'
import {createHtmlPlugin} from 'vite-plugin-html'
import {defineConfig} from 'vite'
import Sonda from 'sonda/vite';
import favicons from "favicons";

async function generateFavicons() {
    const source = "kotlin/images/logo.png";
    const response = await favicons(source, {path: "/images/favicons",});
    const faviconDirectory = 'kotlin/images/favicons';
    await fsp.mkdir(faviconDirectory, {recursive: true});
    await Promise.all(
        response.images.map(
            async (image) =>
                await fsp.writeFile(path.join(faviconDirectory, image.name), image.contents),
        ),
    );
    await Promise.all(
        response.files.map(
            async (file) =>
                await fsp.writeFile(path.join(faviconDirectory, file.name), file.contents),
        ),
    );
    const htmlBasename = "index.html"
    await fsp.writeFile(path.join(faviconDirectory, htmlBasename), response.html.join("\n"));
    return response;
}

function generateCdnInformation(isServing) {
    let cdnFile = fs.readFileSync('cdn.json', {encoding: "UTF-8"});
    if (isServing) {
        cdnFile = cdnFile.replaceAll('production', 'development')
    }
    const cdnResources = JSON.parse(
        cdnFile
            .split(/\r?\n/)
            .filter((line) => !line.includes("TRACE"))
            .join("\n")
    )
    const cdnSettings = JSON.parse(fs.readFileSync('cdn.settings.json'))
    const cdnLibraries = Object.entries(cdnSettings);
    const cdnImportMap = Object.fromEntries(
        cdnLibraries.map(([key, value], index) => [key, cdnResources[key] + '?' + cdnLibraries.slice(0, index).map(([lib]) => `external=${lib}`).join("&") ])
    );
    return {isServing, cdnSettings, cdnImportMap};
}

export default defineConfig(async ({command}) => {
    const isServing = command === 'serve';
    const response = await generateFavicons();
    const {cdnSettings, cdnImportMap} = generateCdnInformation(isServing);
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
        experimental: {
            "renderBuiltUrl": function (filename, {hostType}) {
                if (hostType === 'js') {
                    return {runtime: `window.webpackPublicPath + ${JSON.stringify(filename)}`}
                } else {
                    return {relative: true}
                }
            },
        },
        plugins: [
            createHtmlPlugin({
                template: 'index.html',
                inject: {
                    data: {
                        htmlWebpackPlugin: {
                            options: {
                                title: isServing ? 'Coupling Dev Server' : 'Coupling',
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
                            tags: {
                                headTags: [`<script type="module" src="./Coupling-client.mjs"></script>`, ...response.html].join('\n')
                            }
                        }
                    },
                }
            }),
            Sonda({open: false}),
        ],
    });
})
