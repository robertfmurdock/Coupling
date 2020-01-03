import {
  graphql,
  GraphQLSchema,
  GraphQLObjectType,
  GraphQLString, GraphQLList, GraphQLInt, GraphQLBoolean, GraphQLNonNull,
} from 'graphql';

const PinType = new GraphQLObjectType({
  name: 'Pin',
  description: 'Something to put on your shirt!!',
  fields: () => ({
    _id: {type: GraphQLNonNull(GraphQLString)},
    icon: {type: GraphQLString},
    name: {type: GraphQLString},
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

const PairAssignmentDocumentType = new GraphQLObjectType({
  name: 'PairAssignmentDocument',
  description: 'Assignments!',
  fields: () => ({
    _id: {type: GraphQLNonNull(GraphQLString)},
    date: {
      type: GraphQLNonNull(GraphQLString),
      resolve: async content => content.date.toISOString()
    },
    pairs: {type: new GraphQLList(new GraphQLList(PinnedPlayerType))},
  }),
});

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
    nonsense: {type: GraphQLBoolean},
    pinList: {
      type: new GraphQLList(PinType),
      resolve: async function (tribe, args, request) {
        const dispatcher = await request.commandDispatcher.authorizedDispatcher(tribe.id);
        return await dispatcher.performPinListQueryGQL();
      }
    },
    playerList: {
      type: new GraphQLList(PlayerType),
      resolve: async function (tribe, args, request) {
        const dispatcher = await request.commandDispatcher.authorizedDispatcher(tribe.id);
        return await dispatcher.performPlayerListQueryGQL();
      }
    },
    pairAssignmentDocumentList: {
      type: new GraphQLList(PairAssignmentDocumentType),
      resolve: async function (tribe, args, request) {
        const dispatcher = await request.commandDispatcher.authorizedDispatcher(tribe.id);
        let newVar = await dispatcher.performPairAssignmentDocumentListQueryGQL();
        console.log('server pairs yo', JSON.stringify(newVar))
        return newVar;
      }
    }
  }),
});

const CouplingSchema = new GraphQLSchema({
  query: new GraphQLObjectType({
    name: 'RootQueryType',
    fields: {
      tribeList: {
        type: new GraphQLList(TribeType),
        resolve(root, args, request) {
          return request.commandDispatcher.performTribeListQueryGQL();
        },
      },
      tribe: {
        type: TribeType,
        args: {id: {type: GraphQLString},},
        resolve: function (root, args, request) {
          return request.commandDispatcher.performTribeQueryGQL(args.id);
        }
      }
    },
  }),
});

export default CouplingSchema;