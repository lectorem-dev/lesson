import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

type MarkdownLessonContentProps = {
  markdownContent: string
}

export function MarkdownLessonContent({ markdownContent }: MarkdownLessonContentProps) {
  if (!markdownContent.trim()) {
    return (
      <section className="lesson-markdown lesson-markdown--empty">
        <p>Материалы урока пока пустые.</p>
      </section>
    )
  }

  return (
    <section className="lesson-markdown">
      <article className="markdown-body lesson-markdown__body">
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{markdownContent}</ReactMarkdown>
      </article>
    </section>
  )
}
