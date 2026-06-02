import { splitByDivider, validateBlock, processContent } from './markdown-contract'

jest.mock('./markdown-engine', () => ({
  render: jest.fn().mockReturnValue('<p>rendered</p>')
}))

describe('splitByDivider', () => {
  it('returns single block when no divider', () => {
    expect(splitByDivider('# Hello')).toEqual(['# Hello'])
  })

  it('splits multiple blocks by divider', () => {
    expect(splitByDivider('aaa\n---\nbbb\n---\nccc')).toEqual(['aaa', 'bbb', 'ccc'])
  })

  it('returns empty array for empty string', () => {
    expect(splitByDivider('')).toEqual([])
  })

  it('filters blocks with only whitespace', () => {
    expect(splitByDivider('aaa\n---\n   \n---\nbbb')).toEqual(['aaa', 'bbb'])
  })

  it('trims blocks and filters empty from divider at start and end', () => {
    expect(splitByDivider('\n---\naaa\n---\n')).toEqual(['aaa'])
  })
})

describe('validateBlock', () => {
  it('returns no warnings for valid h1-only content with appropriate length', () => {
    const longEnough = '# Title\n' + 'x'.repeat(80)
    expect(validateBlock(longEnough)).toEqual([])
  })

  it('warns for h2 headings', () => {
    const result = validateBlock('## Subtitle\n' + 'x'.repeat(80))
    expect(result).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'heading' })
      ])
    )
  })

  it('warns for h3 headings', () => {
    const result = validateBlock('### Subtitle\n' + 'x'.repeat(80))
    expect(result).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'heading' })
      ])
    )
  })

  it('warns for text exceeding 500 chars', () => {
    const result = validateBlock('x'.repeat(501))
    expect(result).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'length', message: expect.stringContaining('超过500字') })
      ])
    )
  })

  it('warns for text less than 80 chars but greater than 0', () => {
    const result = validateBlock('short')
    expect(result).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'length', message: expect.stringContaining('不足80字') })
      ])
    )
  })

  it('does not warn about length for empty text', () => {
    const result = validateBlock('')
    const lengthWarnings = result.filter(w => w.type === 'length')
    expect(lengthWarnings).toEqual([])
  })

  it('warns for raw HTML tags', () => {
    const result = validateBlock('<div>hello</div>')
    expect(result).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'html' })
      ])
    )
  })

  it('can return multiple warnings at once', () => {
    const result = validateBlock('## heading\n<div>html</div>')
    const types = result.map(w => w.type)
    expect(types).toContain('heading')
    expect(types).toContain('html')
    expect(types).toContain('length')
  })
})

describe('processContent', () => {
  it('returns correct structure with index, html, and warnings', () => {
    const blockA = '# Block A\n' + 'x'.repeat(80)
    const blockB = '## Block B'
    const result = processContent(blockA + '\n---\n' + blockB)
    expect(result).toHaveLength(2)
    expect(result[0]).toEqual({
      html: '<p>rendered</p>',
      index: 1,
      warnings: []
    })
    expect(result[1].index).toBe(2)
    expect(result[1].html).toBe('<p>rendered</p>')
    expect(result[1].warnings).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'heading' })
      ])
    )
  })
})
