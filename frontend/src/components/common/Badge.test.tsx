import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Badge } from './Badge';

describe('Badge Component', () => {
  it('should render children correctly', () => {
    render(<Badge>Test Badge</Badge>);
    expect(screen.getByText('Test Badge')).toBeInTheDocument();
  });

  it('should apply default variant styling', () => {
    const { container } = render(<Badge>Default</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#028090]/10');
  });

  it('should apply success variant styling', () => {
    const { container } = render(<Badge variant="success">Success</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#00A896]/10');
  });

  it('should apply error variant styling', () => {
    const { container } = render(<Badge variant="error">Error</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#E45858]/10');
  });

  it('should apply warning variant styling', () => {
    const { container } = render(<Badge variant="warning">Warning</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#F0A93C]/10');
  });

  it('should apply custom className', () => {
    const { container } = render(<Badge className="custom-class">Custom</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('custom-class');
  });

  it('should handle HTTP status codes - 2xx', () => {
    const { container } = render(<Badge codeHttp={200}>OK</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#00A896]/10');
  });

  it('should handle HTTP status codes - 4xx', () => {
    const { container } = render(<Badge codeHttp={404}>Not Found</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#F0A93C]/10');
  });

  it('should handle HTTP status codes - 5xx', () => {
    const { container } = render(<Badge codeHttp={500}>Server Error</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-[#E45858]/10');
  });

  it('should handle null HTTP code', () => {
    const { container } = render(<Badge codeHttp={null}>No Code</Badge>);
    const badge = container.querySelector('span');
    expect(badge).toHaveClass('bg-slate-100');
  });
});