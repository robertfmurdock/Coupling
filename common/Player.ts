import Badge from "./Badge";
interface Player {
    _id?: any;
    name?: string;
    email?: string;
    callSignAdjective?: string;
    callSignNoun?: string;
    badge?: Badge,
    pins?: string[],
    imageURL?: string
}
export default Player