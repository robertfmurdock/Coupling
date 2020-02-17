import {
  GraphQLBoolean,
  GraphQLInt,
  GraphQLFloat,
  GraphQLList,
  GraphQLNonNull,
  GraphQLObjectType,
  GraphQLSchema,
  GraphQLString,
} from 'graphql';
import * as server from "Coupling-server";

const PinType = new GraphQLObjectType({
  name: 'Pin',
  description: 'Something to put on your shirt!!',
  fields: () => ({
    _id: {type: GraphQLNonNull(GraphQLString)},
    icon: {type: GraphQLString},
    name: {type: GraphQLString},
    modifyingUserEmail: {type: GraphQLString},
    timestamp: {type: GraphQLString}
  }),
});

const PlayerType = new GraphQLObjectType({
  name: 'Player',
  description: 'Weirdos who want to couple',
  fields: () => ({
    _id: {type: GraphQLNonNull(GraphQLString)},
    name: {type: GraphQLString},
    email: {type: GraphQLString},
    badge: {type: GraphQLString},
    callSignAdjective: {type: GraphQLString},
    callSignNoun: {type: GraphQLString},
    imageURL: {type: GraphQLString},
    modifyingUserEmail: {type: GraphQLString},
    timestamp: {type: GraphQLString}
  }),
});

const PinnedPlayerType = new GraphQLObjectType({
  name: 'PinnedPlayer',
  description: '',
  fields: () => ({
    _id: {type: GraphQLString},
    name: {type: GraphQLString},
    email: {type: GraphQLString},
    badge: {type: GraphQLString},
    callSignAdjective: {type: GraphQLString},
    callSignNoun: {type: GraphQLString},
    imageURL: {type: GraphQLString},
    pins: {type: new GraphQLList(PinType)}
  }),
});

const PinnedPairType = new GraphQLObjectType({
  name: "PinnedPair",
  fields: {
    players: {type: new GraphQLList(PinnedPlayerType)},
    pins: {type: new GraphQLList(PinType)}
  }
});

const PairAssignmentDocumentType = new GraphQLObjectType({
  name: 'PairAssignmentDocument',
  description: 'Assignments!',
  fields: () => ({
    _id: {type: GraphQLNonNull(GraphQLString)},
    date: {
      type: GraphQLNonNull(GraphQLString),
      resolve: async content => content.date.toISOString()
    },
    pairs: {
      type: new GraphQLList(PinnedPairType)
    },
  }),
});

// noinspection JSUnresolvedVariable
const {Resolvers} = server.com.zegreatrob.coupling.server.entity;

const TribeType = new GraphQLObjectType({
  name: 'Tribe',
  description: 'The people you couple with!',
  fields: () => ({
    id: {type: GraphQLNonNull(GraphQLString)},
    name: {type: GraphQLString},
    email: {type: GraphQLString},
    pairingRule: {type: GraphQLInt},
    defaultBadgeName: {type: GraphQLString},
    alternateBadgeName: {type: GraphQLString},
    badgesEnabled: {type: GraphQLBoolean},
    callSignsEnabled: {type: GraphQLBoolean},
    animationsEnabled: {type: GraphQLBoolean},
    animationSpeed: {type: GraphQLFloat},
    pinList: {
      type: new GraphQLList(PinType),
      resolve: Resolvers.pinList
    },
    playerList: {
      type: new GraphQLList(PlayerType),
      resolve: Resolvers.playerList
    },
    pairAssignmentDocumentList: {
      type: new GraphQLList(PairAssignmentDocumentType),
      resolve: Resolvers.pairAssignmentDocumentList
    }
  }),
});

const CouplingSchema = new GraphQLSchema({
  query: new GraphQLObjectType({
    name: 'RootQueryType',
    fields: {
      tribeList: {
        type: new GraphQLList(TribeType),
        resolve: Resolvers.tribeList,
      },
      tribe: {
        type: TribeType,
        args: {id: {type: GraphQLString},},
        resolve: Resolvers.tribe
      }
    },
  }),
});

export default CouplingSchema;