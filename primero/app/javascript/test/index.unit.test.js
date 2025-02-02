import * as moduleToTest from "./index";

describe("app/javascript/test/index", () => {
  const clone = { ...moduleToTest };

  after(() => {
    expect(clone).to.be.empty;
  });

  [
    "abbrMonthNames",
    "setupMockFieldComponent",
    "setupMockFormComponent",
    "setupMountedComponent",
    "setupMountedThemeComponent",
    "spy",
    "stub",
    "fake",
    "tick",
    "replace",
    "useFakeTimers",
    "mock",
    "createSimpleMount",
    "createMockStore",
    "createMiddleware",
    "listHeaders",
    "lookups",
    "translateOptions",
    "setupHook",
    "FormikValueFromHook"
  ].forEach(property => {
    it(`exports property '${property}'`, () => {
      expect(clone).to.have.property(property);
      delete clone[property];
    });
  });
});
