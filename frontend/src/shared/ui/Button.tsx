import type { ButtonHTMLAttributes, ReactNode } from 'react'

// Добавлен вариант 'outline'
type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode
  variant?: ButtonVariant
}

export function Button({ children, className = '', variant = 'primary', ...props }: ButtonProps) {
  return (
    <button className={`button button--${variant} ${className}`.trim()} {...props}>
      {children}
    </button>
  )
}