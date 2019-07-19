// @ts-ignore
import {components} from 'client'

export default class GoogleSignIn {
    static async checkForSignedIn() {
        return await components.googleCheckForSignedIn()
    }
}