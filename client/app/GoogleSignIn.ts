import axios from "axios";

export default class GoogleSignIn {

    static async signIn() {
        const googleAuth = await this.getGoogleAuth();
        const user = await this.performSignIn(googleAuth);
        await this.createSession(user);
        window.location.pathname = "/"
    }

    private static async createSession(user) {
        const idToken = user.getAuthResponse().id_token;
        await axios.post(`/auth/google-token`, {idToken: idToken});
    }

    static async checkForSignedIn() {
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

    private static async performSignIn(googleAuth) {
        const isSignedIn = googleAuth.isSignedIn.get();
        if (!isSignedIn) {
            return await googleAuth.signIn({
                scope: 'profile email',
                prompt: 'consent',
                ux_mode: 'redirect',
                redirect_uri: window.location.origin
            });
        } else {
            return await googleAuth.currentUser.get();
        }
    }

}