import {
  graphql,
  GraphQLSchema,
  GraphQLObjectType,
  GraphQLString, GraphQLList, GraphQLInt, GraphQLBoolean,
} from 'graphql';

const PinType = new GraphQLObjectType({
  name: 'Pin',
  description: 'Something to put on your shirt!!',
  fields: () => ({
    _id: {type: GraphQLString},
    icon: {type: GraphQLString},
    name: {type: GraphQLString},
  }),
});

const TribeType = new GraphQLObjectType({
  name: 'Tribe',
  description: 'The people you couple with!',
  fields: () => ({
    id: {type: GraphQLString},
    name: {type: GraphQLString},
    email: {type: GraphQLString},
    pairingRule: {type: GraphQLInt},
    defaultBadgeName: {type: GraphQLString},
    alternateBadgeName: {type: GraphQLString},
    badgesEnabled: {type: GraphQLBoolean},
    callSignsEnabled: {type: GraphQLBoolean},
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
      },
      pinList: {
        type: new GraphQLList(PinType),
        args: {tribeId: {type: GraphQLString},},
        async resolve(root, args, request) {
          const dispatcher = await request.commandDispatcher.authorizedDispatcher(args.tribeId);
          return await dispatcher.performPinListQueryGQL();
        },
      }
    },
  }),
});

export default CouplingSchema;