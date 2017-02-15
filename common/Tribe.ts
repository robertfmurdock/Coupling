import PairingRule from './PairingRule'

interface Tribe {
    id: string;
    name: string;
    badgesEnabled?: boolean;
    defaultBadgeName?: string;
    alternateBadgeName?: string;
    pairingRule?: PairingRule
}

export default Tribe