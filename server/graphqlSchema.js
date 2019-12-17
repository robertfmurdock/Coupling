// @ts-ignore
import * as server from "Coupling-server";

import {
  graphql,
  GraphQLSchema,
  GraphQLObjectType,
  GraphQLString, GraphQLList, GraphQLInt, GraphQLBoolean,
} from 'graphql';

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
        args: {
          id: {type: GraphQLString},
        },
        resolve: (root, args, request) => request.commandDispatcher.performTribeQueryGQL(args.id)
      },
    },
  }),
});

export default CouplingSchema;