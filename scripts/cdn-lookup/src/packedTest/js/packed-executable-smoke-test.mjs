import {execFileSync} from "node:child_process"
import {mkdirSync, rmSync, writeFileSync} from "node:fs"
import path from "node:path"

const [archive, fixture] = process.argv.slice(2)
rmSync(fixture, {recursive: true, force: true})
mkdirSync(fixture, {recursive: true})
writeFileSync(
    path.join(fixture, "package.json"),
    JSON.stringify({private: true, dependencies: {"resolve-pkg": "3.0.1"}}),
)
execFileSync("npm", ["install", "--ignore-scripts", archive], {cwd: fixture, stdio: "pipe"})

const binary = path.join(fixture, "node_modules", ".bin", "cdn-lookup")
const output = execFileSync(binary, ["resolve-pkg"], {cwd: fixture, encoding: "utf8"})
const result = JSON.parse(output)
if (result.urls["resolve-pkg"] !== "https://esm.sh/resolve-pkg@3.0.1") {
    throw new Error(`Unexpected lookup result: ${output}`)
}
