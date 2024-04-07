## TODO

- Time Management
    - Error handling -> remove worker and call a error routine at the worker
    - add ui observer
    - register worker during execution
    - remove worker -> testing
    - run modification in context of time management (e.g. modify stoptime add worker etc -> stop exeuction to avoid wallclock weirdness)

## Hints

- Determine the greatest possible lookahead:
  org.apache.commons.math3.util:
  val biggestLookahead = workers.values
  .map { it.lookahead.timeSpan }
  .distinct()
  .reduce { a, b -> ArithmeticUtils.gcd(a, b) }