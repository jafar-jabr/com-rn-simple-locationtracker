import { NativeModules } from 'react-native';

const { SimpleLocationTracker } = NativeModules;

const unBindService = () => {
  SimpleLocationTracker.releaseTheService();
};

const isRunning = () => {
  return new Promise((resolve) => {
    SimpleLocationTracker.isRunning((result: boolean) => {
      return resolve(result);
    });
  });
};

const startLocationTracking = (interval: number) => {
  isRunning().then((isServiceRunning) => {
    if (isServiceRunning) {
      return;
    }
    SimpleLocationTracker.initializeLocationTracker(interval);
    setTimeout(() => {
      SimpleLocationTracker.startObserving();
    }, 1000);
    setTimeout(() => {
      unBindService();
    }, 2000);
  });
};

const stopLocationTracking = () => {
  isRunning().then((isServiceRunning) => {
    if (!isServiceRunning) {
      return;
    }
    SimpleLocationTracker.stopObserving();
  });
};

const getLastKnownLocation = () => {
  return new Promise((resolve) => {
    SimpleLocationTracker.getLastGeoLocation((location: boolean) => {
      if (location) {
        return resolve(location);
      }
      return resolve({ longitude: 0, latitude: 0, isFromMockProvider: false });
    });
  });
};

const isLocationEnabled = () => {
  return new Promise((resolve) => {
    SimpleLocationTracker.isLocationEnabledInSettings((isEnabled: boolean) => {
      return resolve(isEnabled);
    });
  });
};

const isLocationEnabledSecondCheck = () => {
  return new Promise((resolve) => {
    SimpleLocationTracker.checkIfLocationOpened((isEnabled: boolean) => {
      return resolve(isEnabled);
    });
  });
};

const changeLocationSettings = () => {
  SimpleLocationTracker.goToLocationSettings();
};

export default {
  startLocationTracking,
  stopLocationTracking,
  getLastKnownLocation,
  isLocationEnabled,
  changeLocationSettings,
  isLocationEnabledSecondCheck,
};
