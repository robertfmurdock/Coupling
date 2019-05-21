import Player from "../../../../common/Player";
import Tribe from "../../../../common/Tribe";
import * as md5 from 'blueimp-md5'

function gravatarUrl(email, options) {
    const codedEmail = md5(email.toLowerCase().trim());
    return `https://www.gravatar.com/avatar/${codedEmail}?default=${options.default}&s=${options.size}`;
}

export function playerGravatarUrl(player: Player, options) {
    if (player && player.imageURL) {
        return player.imageURL;
    } else {
        options['default'] = "retro";
        let email = "";
        if (player) {
            email = player.email ? player.email : player.name || '';
        }
        return gravatarUrl(email, options);
    }
}

export function tribeGravatarUrl(tribe: Tribe, options) {
    if (tribe && tribe.email) {
        options['default'] = "identicon";
        return gravatarUrl(tribe.email, options);
    } else {
        return "/images/icons/tribes/no-tribe.png";
    }
}