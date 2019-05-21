import * as md5 from 'blueimp-md5'

export function gravatarUrl(email, options) {
    const codedEmail = md5(email.toLowerCase().trim());
    return `https://www.gravatar.com/avatar/${codedEmail}?default=${options.default}&s=${options.size}`;
}
