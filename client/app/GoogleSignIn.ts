// @ts-ignore
import {googleCheckForSignedIn} from 'client'

export default class GoogleSignIn {
    static async checkForSignedIn() {
        return await googleCheckForSignedIn()
    }
}