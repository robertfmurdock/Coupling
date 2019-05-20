import Player from "../../../../common/Player";
import * as md5 from 'blueimp-md5'

export function gravatarUrl(player: Player, options) {
    if (player && player.imageURL) {
        return player.imageURL;
    } else {
        options['default'] = "retro";
        let email = "";
        if (player) {
            email = player.email ? player.email : player.name || '';
        }

        const codedEmail = md5(email.toLowerCase().trim());

        return `https://www.gravatar.com/avatar/${codedEmail}?default=retro&s=${options.size}`;
    }
}