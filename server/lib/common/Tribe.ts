import PairingRule from './PairingRule'

interface Tribe {
    id: string;
    name: string;
    badgesEnabled?: boolean;
    callSignsEnabled?: boolean;
    defaultBadgeName?: string;
    alternateBadgeName?: string;
    email?: string;
    pairingRule?: PairingRule
}

export default Tribe