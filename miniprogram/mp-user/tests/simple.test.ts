describe('Simple Test', () => {
  it('should pass', () => {
    expect(1 + 1).toBe(2);
  });

  it('should work with async', async () => {
    const promise = Promise.resolve('test');
    await expect(promise).resolves.toBe('test');
  });
});
