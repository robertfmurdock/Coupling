import Badge from "./Badge";
interface Player {
    _id: any;
    tribe: string;
    name?: string;
    email?: string;
    callSignAdjective?: string;
    callSignNoun?: string;
    badge?: Badge
}
export default Player