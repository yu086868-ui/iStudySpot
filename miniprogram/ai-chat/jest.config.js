/** @type {import('jest').Config} */
const config = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/miniprogram'],
  testMatch: ['**/__tests__/**/*.test.ts'],
  moduleFileExtensions: ['ts', 'js', 'json'],
  collectCoverageFrom: [
    'miniprogram/**/*.ts',
    '!miniprogram/**/*.d.ts',
    '!miniprogram/app.ts',
    '!miniprogram/pages/**/*.ts',
    '!miniprogram/components/**/*.ts'
  ],
  coverageDirectory: 'coverage',
  coverageReporters: ['text', 'lcov', 'html'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/miniprogram/$1'
  },
  setupFilesAfterEnv: ['<rootDir>/miniprogram/__tests__/setup.ts'],
  transform: {
    '^.+\\.ts$': ['ts-jest', {
      tsconfig: 'tsconfig.test.json'
    }]
  },
  verbose: true,
  testTimeout: 10000
};

module.exports = config;
