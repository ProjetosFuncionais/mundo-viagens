import { flightsApi } from './services';
export type { Flight, Hotel } from './services';
export const searchFlights = flightsApi.search;
export const getFlightById = flightsApi.get;
