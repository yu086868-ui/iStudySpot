const isCI = process.env.CI === 'true' || process.env.CI === '1';

module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/tests'],
  testMatch: ['**/*.test.ts', '**/*.spec.ts'],
  collectCoverageFrom: [
    'miniprogram/utils/**/*.ts',
    'miniprogram/services/**/*.ts',
    '!miniprogram/**/*.d.ts',
    '!miniprogram/utils/mock.ts',
    '!miniprogram/utils/mock-data.ts',
    '!miniprogram/utils/data.ts',
    '!miniprogram/utils/request.ts'
  ],
  coverageDirectory: 'coverage',
  coverageReporters: ['text', 'lcov', 'html'],
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70
    }
  },
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/miniprogram/$1',
    '^../miniprogram/(.*)$': '<rootDir>/miniprogram/$1',
    '^../tests/(.*)$': '<rootDir>/tests/$1'
  },
  setupFilesAfterEnv: ['<rootDir>/tests/setup.ts'],
  moduleDirectories: ['node_modules', '<rootDir>'],
  rootDir: './',
  modulePathIgnorePatterns: ['<rootDir>/dist/'],
  transform: {
    '^.+\\.tsx?$': ['ts-jest', {
      tsconfig: 'tsconfig.test.json'
    }]
  },
  testTimeout: isCI ? 15000 : 5000,
  verbose: true,
  detectOpenHandles: isCI,
  forceExit: isCI,
  reporters: isCI 
    ? ['default', ['jest-junit', { outputDirectory: './reports', outputName: 'junit.xml' }]]
    : ['default']
};
