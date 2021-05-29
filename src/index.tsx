import { NativeModules } from 'react-native';

type ComRnSimpleLocationtrackerType = {
  multiply(a: number, b: number): Promise<number>;
};

const { ComRnSimpleLocationtracker } = NativeModules;

export default ComRnSimpleLocationtracker as ComRnSimpleLocationtrackerType;
