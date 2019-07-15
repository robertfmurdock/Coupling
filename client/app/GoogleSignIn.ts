import axios from "axios";
import waitFor from "../test/WaitFor";

export default class GoogleSignIn {

    private static async createSession(user) {
        const idToken = user.getAuthResponse().id_token;
        await axios.post(`/auth/google-token`, {idToken: idToken});
    }

    static async checkForSignedIn() {
        // @ts-ignore
        await waitFor(()=> window.isAuthenticated !== undefined, 100);
        // @ts-ignore
        if (window.isAuthenticated) {
            return true
        }

        const googleAuth = await this.getGoogleAuth();
        const isSignedIn = googleAuth.isSignedIn.get();
        if (isSignedIn) {
            const user = await googleAuth.currentUser.get();
            await this.createSession(user)
        }
        return isSignedIn;
    }

    static async signOut() {
        const googleAuth = await this.getGoogleAuth();
        const isSignedIn = googleAuth.isSignedIn.get();
        if (isSignedIn) {
            await googleAuth.signOut();
        }
    }

    private static async getGoogleAuth() {
        let auth2 = await this.loadGoogleAuth2();

        return await auth2.init({
            // @ts-ignore
            client_id: window.googleClientId
        });
    }

    private static async loadGoogleAuth2(): Promise<any> {
        return await new Promise((resolve) => {
            // @ts-ignore
            gapi.load('auth2', function () {
                // @ts-ignore
                resolve(gapi.auth2)
            })
        });
    }

}